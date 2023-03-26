import { Component } from '@angular/core';
import { NgForm, NgModel } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { Observable, of } from 'rxjs';
import { Owner } from 'src/app/dto/owner';
import { OwnerService } from 'src/app/service/owner.service';

@Component({
  selector: 'app-owner-add',
  templateUrl: './owner-add.component.html',
  styleUrls: ['./owner-add.component.scss'],
})
export class OwnerAddComponent {
  owner: Owner = {
    firstName: '',
    lastName: '',
    email: undefined,
  };

  constructor(
    private service: OwnerService,
    private router: Router,
    private notification: ToastrService
  ) {}

  public dynamicCssClassesForInput(input: NgModel): any {
    return {
      // This names in this object are determined by the style library,
      // requiring it to follow TypeScript naming conventions does not make sense.
      // eslint-disable-next-line @typescript-eslint/naming-convention
      'is-invalid': !input.valid && !input.pristine,
    };
  }

  public onSubmit(form: NgForm): void {
    console.log('is form valid?', form.valid, this.owner);
    if (form.valid) {
      const observable: Observable<Owner> = this.service.create(this.owner);
      observable.subscribe({
        next: () => {
          this.notification.success(
            `Owner ${this.owner.firstName} ${this.owner.lastName} successfully created.`
          );
          this.router.navigate(['/owners']);
        },
        error: (error) => {
          console.error('Error creating owner', error);
          this.errorHandler(error);
        },
      });
    }
  }

  private errorHandler(
    error: any,
    action: string | undefined = undefined
  ): void {
    if (error.status === 0) {
      this.notification.error('Backend is not reachable');
    } else if (error.status === 404) {
      this.notification.error(
        `Could not ${action} horse - ` + error.error.error
      );
    } else if (error.status === 409 || error.status === 422) {
      for (const err of error.error.errors) {
        this.notification.error(err);
      }
    } else if (error.status >= 500) {
      this.notification.error(
        error.error.error + ': Something unexpected happend!'
      );
    }
  }
}
